# ProFitness Backend (Node.js + Express + MongoDB)

This backend is designed for your Android Java app.
It provides JWT auth, user profile, workouts, hydration, nutrition, exercise library, and dashboard summary endpoints.

## 1) Environment setup
1. Copy `.env.example` to `.env`.
2. Set `MONGODB_URI` with a working Atlas or local Mongo connection.
3. Set `JWT_SECRET` to a long random string.

Example local URI:

`mongodb://127.0.0.1:27017/profitness`

## 2) Run backend
1. `cd backend`
2. `npm install`
3. `npm run dev`

Server default URL: `http://localhost:5000`

## 3) Auth flow for Android Java
1. Register or login to get a JWT token.
2. Save token locally (SharedPreferences).
3. Send header on protected routes:

`Authorization: Bearer <token>`

## 4) API endpoints

Public routes:
- `GET /api/health`
- `GET /api/exercises`
- `POST /api/auth/register`
- `POST /api/auth/login`

Protected routes (require Bearer token):
- `GET /api/auth/me`
- `PUT /api/users/me`
- `GET /api/workouts`
- `GET /api/workouts/:id`
- `POST /api/workouts`
- `PUT /api/workouts/:id`
- `DELETE /api/workouts/:id`
- `GET /api/hydration`
- `POST /api/hydration`
- `GET /api/hydration/today-total`
- `GET /api/nutrition`
- `POST /api/nutrition`
- `GET /api/nutrition/today-summary`
- `GET /api/dashboard/summary`

## 5) Sample request payloads

Register:

```json
{
  "name": "Marcelo",
  "email": "marcelo@example.com",
  "password": "secret123"
}
```

Create workout:

```json
{
  "workoutName": "Push Day",
  "durationMinutes": 52,
  "caloriesBurned": 420
}
```

Create hydration log:

```json
{
  "amountMl": 500
}
```

Create nutrition log:

```json
{
  "mealName": "Chicken Rice",
  "calories": 650,
  "proteinGrams": 45,
  "carbsGrams": 60,
  "fatGrams": 18
}
```

## 6) Android Java base URL notes
- Android emulator to local machine: `http://10.0.2.2:5000`
- Physical device on same Wi-Fi: `http://<your-computer-lan-ip>:5000`

## 7) Troubleshooting
- `MONGODB_URI still contains placeholders`: update `.env` with a real URI.
- `querySrv ECONNREFUSED`: use non-SRV atlas URI (`mongodb://...`) if your DNS/network blocks SRV lookups.
- `Invalid or expired token`: login again and replace saved token.
