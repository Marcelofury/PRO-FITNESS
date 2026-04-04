param(
    [string]$BaseUrl = "http://127.0.0.1:5000"
)

$ErrorActionPreference = "Stop"

function Run-Test {
    param(
        [string]$Name,
        [scriptblock]$Action
    )

    try {
        $result = & $Action
        Write-Host ("PASS " + $Name) -ForegroundColor Green
        return [pscustomobject]@{ Name = $Name; Passed = $true; Result = $result; Error = $null }
    }
    catch {
        $message = $_.Exception.Message
        Write-Host ("FAIL " + $Name + " :: " + $message) -ForegroundColor Red
        return [pscustomobject]@{ Name = $Name; Passed = $false; Result = $null; Error = $message }
    }
}

$stamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$email = "apitest_$stamp@profitness.local"
$password = "Secret123!"
$headers = @{}
$workoutId = ""

Write-Host "Smoke test base URL: $BaseUrl" -ForegroundColor Cyan
Write-Host "Test account email: $email" -ForegroundColor Cyan

$results = @()

$results += Run-Test -Name "health" -Action {
    Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/health"
}

$results += Run-Test -Name "auth.register" -Action {
    Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/auth/register" -ContentType "application/json" -Body (@{
        name = "API Test"
        email = $email
        password = $password
    } | ConvertTo-Json)
}

$results += Run-Test -Name "auth.login" -Action {
    $login = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/auth/login" -ContentType "application/json" -Body (@{
        email = $email
        password = $password
    } | ConvertTo-Json)

    $script:headers = @{ Authorization = "Bearer $($login.data.token)" }
    $login
}

$results += Run-Test -Name "auth.me" -Action {
    Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/auth/me" -Headers $headers
}

$results += Run-Test -Name "users.me.update" -Action {
    Invoke-RestMethod -Method Put -Uri "$BaseUrl/api/users/me" -Headers $headers -ContentType "application/json" -Body (@{
        age = 26
        heightCm = 178
        weightKg = 74
        goal = "Lean muscle"
    } | ConvertTo-Json)
}

$results += Run-Test -Name "workouts.create" -Action {
    $created = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/workouts" -Headers $headers -ContentType "application/json" -Body (@{
        workoutName = "Smoke Workout"
        durationMinutes = 42
        caloriesBurned = 320
    } | ConvertTo-Json)

    $script:workoutId = $created.data._id
    $created
}

$results += Run-Test -Name "workouts.list" -Action {
    Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/workouts" -Headers $headers
}

$results += Run-Test -Name "workouts.getById" -Action {
    Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/workouts/$workoutId" -Headers $headers
}

$results += Run-Test -Name "workouts.update" -Action {
    Invoke-RestMethod -Method Put -Uri "$BaseUrl/api/workouts/$workoutId" -Headers $headers -ContentType "application/json" -Body (@{
        workoutName = "Smoke Workout Updated"
        durationMinutes = 45
        caloriesBurned = 350
    } | ConvertTo-Json)
}

$results += Run-Test -Name "hydration.create" -Action {
    Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/hydration" -Headers $headers -ContentType "application/json" -Body (@{
        amountMl = 500
    } | ConvertTo-Json)
}

$results += Run-Test -Name "hydration.list" -Action {
    Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/hydration" -Headers $headers
}

$results += Run-Test -Name "hydration.today-total" -Action {
    Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/hydration/today-total" -Headers $headers
}

$results += Run-Test -Name "nutrition.create" -Action {
    Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/nutrition" -Headers $headers -ContentType "application/json" -Body (@{
        mealName = "API Meal"
        calories = 640
        proteinGrams = 48
        carbsGrams = 62
        fatGrams = 18
    } | ConvertTo-Json)
}

$results += Run-Test -Name "nutrition.list" -Action {
    Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/nutrition" -Headers $headers
}

$results += Run-Test -Name "nutrition.today-summary" -Action {
    Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/nutrition/today-summary" -Headers $headers
}

$results += Run-Test -Name "exercises.list" -Action {
    Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/exercises"
}

$results += Run-Test -Name "dashboard.summary" -Action {
    Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/dashboard/summary" -Headers $headers
}

if ($workoutId) {
    $results += Run-Test -Name "workouts.delete" -Action {
        Invoke-RestMethod -Method Delete -Uri "$BaseUrl/api/workouts/$workoutId" -Headers $headers
    }
}

$failed = $results | Where-Object { -not $_.Passed }

Write-Host ""
Write-Host "----- Smoke Test Summary -----" -ForegroundColor Yellow
Write-Host ("Passed: " + ($results.Count - $failed.Count))
Write-Host ("Failed: " + $failed.Count)

if ($failed.Count -gt 0) {
    Write-Host ""
    Write-Host "Failed tests:" -ForegroundColor Red
    $failed | ForEach-Object {
        Write-Host (" - " + $_.Name + " :: " + $_.Error) -ForegroundColor Red
    }
    exit 1
}

exit 0
