const express = require('express');
const auth = require('../middleware/auth');
const WorkoutLog = require('../models/WorkoutLog');
const NutritionLog = require('../models/NutritionLog');
const HydrationLog = require('../models/HydrationLog');

const router = express.Router();

router.use(auth);

function computeCurrentStreak(workoutDayStrings) {
  if (!Array.isArray(workoutDayStrings) || workoutDayStrings.length === 0) {
    return 0;
  }

  const workoutSet = new Set(workoutDayStrings);
  const current = new Date();
  current.setHours(0, 0, 0, 0);

  let streak = 0;
  while (true) {
    const key = current.toISOString().slice(0, 10);
    if (!workoutSet.has(key)) {
      break;
    }

    streak += 1;
    current.setDate(current.getDate() - 1);
  }

  return streak;
}

router.get('/summary', async (req, res) => {
  try {
    const dayStart = new Date();
    dayStart.setHours(0, 0, 0, 0);

    const weekStart = new Date(dayStart);
    weekStart.setDate(weekStart.getDate() - 6);

    const monthStart = new Date(dayStart);
    monthStart.setDate(monthStart.getDate() - 29);

    const [dailyWorkoutAgg, weeklyWorkoutAgg, monthlyWorkoutAgg, hydrationAgg, nutritionAgg, recentWorkouts, workoutDays] = await Promise.all([
      WorkoutLog.aggregate([
        { $match: { userId: req.user._id, createdAt: { $gte: dayStart } } },
        {
          $group: {
            _id: null,
            workoutsCount: { $sum: 1 },
            totalWorkoutMinutes: { $sum: '$durationMinutes' },
            totalWorkoutCalories: { $sum: '$caloriesBurned' },
          },
        },
      ]),
      WorkoutLog.aggregate([
        { $match: { userId: req.user._id, createdAt: { $gte: weekStart } } },
        {
          $group: {
            _id: null,
            workoutsCount: { $sum: 1 },
            totalWorkoutMinutes: { $sum: '$durationMinutes' },
          },
        },
      ]),
      WorkoutLog.aggregate([
        { $match: { userId: req.user._id, createdAt: { $gte: monthStart } } },
        {
          $group: {
            _id: null,
            workoutsCount: { $sum: 1 },
            totalWorkoutMinutes: { $sum: '$durationMinutes' },
          },
        },
      ]),
      HydrationLog.aggregate([
        { $match: { userId: req.user._id, loggedAt: { $gte: dayStart } } },
        { $group: { _id: null, totalHydrationMl: { $sum: '$amountMl' } } },
      ]),
      NutritionLog.aggregate([
        { $match: { userId: req.user._id, loggedAt: { $gte: dayStart } } },
        { $group: { _id: null, totalNutritionCalories: { $sum: '$calories' } } },
      ]),
      WorkoutLog.find({ userId: req.user._id }).sort({ createdAt: -1 }).limit(5),
      WorkoutLog.aggregate([
        { $match: { userId: req.user._id } },
        {
          $project: {
            day: {
              $dateToString: { format: '%Y-%m-%d', date: '$createdAt' },
            },
          },
        },
        { $group: { _id: '$day' } },
      ]),
    ]);

    const streakDays = computeCurrentStreak(workoutDays.map((d) => d._id));

    const summary = {
      workoutsCount: dailyWorkoutAgg[0]?.workoutsCount || 0,
      totalWorkoutMinutes: dailyWorkoutAgg[0]?.totalWorkoutMinutes || 0,
      totalWorkoutCalories: dailyWorkoutAgg[0]?.totalWorkoutCalories || 0,
      weeklyWorkoutsCount: weeklyWorkoutAgg[0]?.workoutsCount || 0,
      weeklyWorkoutMinutes: weeklyWorkoutAgg[0]?.totalWorkoutMinutes || 0,
      monthlyWorkoutsCount: monthlyWorkoutAgg[0]?.workoutsCount || 0,
      monthlyWorkoutMinutes: monthlyWorkoutAgg[0]?.totalWorkoutMinutes || 0,
      streakDays,
      totalHydrationMl: hydrationAgg[0]?.totalHydrationMl || 0,
      totalNutritionCalories: nutritionAgg[0]?.totalNutritionCalories || 0,
      recentWorkouts,
    };

    return res.json({ success: true, data: summary });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Failed to fetch dashboard summary' });
  }
});

module.exports = router;
