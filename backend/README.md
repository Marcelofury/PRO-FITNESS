# ProFitness Backend (Node.js + Express + MongoDB Atlas)

## 1) Create MongoDB Atlas online cluster
1. Sign in at MongoDB Atlas.
2. Create a new project and cluster (M0 free tier is fine).
3. Create a database user (username + password).
4. In Network Access, allow your current IP (or 0.0.0.0/0 for testing only).
5. Copy the connection string and replace placeholders.

Example:

`mongodb+srv://<username>:<password>@<cluster-name>.mongodb.net/profitness?retryWrites=true&w=majority&appName=ProFitnessCluster`

## 2) Run backend
1. `cd backend`
2. `copy .env.example .env`
3. Update `MONGODB_URI` in `.env`
4. `npm install`
5. `npm run dev`

## 3) Test API
- Health: `GET http://localhost:5000/api/health`
- List workouts: `GET http://localhost:5000/api/workouts`
- Get one workout: `GET http://localhost:5000/api/workouts/:id`
- Create workout: `POST http://localhost:5000/api/workouts`
- Update workout: `PUT http://localhost:5000/api/workouts/:id`
- Delete workout: `DELETE http://localhost:5000/api/workouts/:id`

Required payload fields for create/update:
- `userId` (string)
- `workoutName` (string)
- `durationMinutes` (positive number)

Optional:
- `caloriesBurned` (non-negative number)

## Troubleshooting
- If startup fails with `MONGODB_URI still contains placeholders`, open `.env` and replace `<username>`, `<password>`, and `<cluster-name>` with real values from MongoDB Atlas.
- If startup fails with `querySrv EBADNAME`, your `.env` still has an invalid Atlas host value.
- If you use a local MongoDB instance instead of Atlas, set `MONGODB_URI=mongodb://127.0.0.1:27017/profitness`.

Sample POST body:

```json
{
  "userId": "user-1",
  "workoutName": "Push Day",
  "durationMinutes": 52,
  "caloriesBurned": 420
}
```
