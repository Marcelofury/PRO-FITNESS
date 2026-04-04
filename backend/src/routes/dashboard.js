const express = require('express');
const auth = require('../middleware/auth');
const WorkoutLog = require('../models/WorkoutLog');
const NutritionLog = require('../models/NutritionLog');
const HydrationLog = require('../models/HydrationLog');

const router = express.Router();

router.use(auth);

router.get('/summary', async (req, res) => {
  try {
    const start = new Date();
    start.setHours(0, 0, 0, 0);

    const [workoutAgg, hydrationAgg, nutritionAgg, recentWorkouts] = await Promise.all([
      WorkoutLog.aggregate([
        { $match: { userId: req.user._id, createdAt: { $gte: start } } },
        {
          $group: {
            _id: null,
            workoutsCount: { $sum: 1 },
            totalWorkoutMinutes: { $sum: '$durationMinutes' },
            totalWorkoutCalories: { $sum: '$caloriesBurned' },
          },
        },
      ]),
      HydrationLog.aggregate([
        { $match: { userId: req.user._id, loggedAt: { $gte: start } } },
        { $group: { _id: null, totalHydrationMl: { $sum: '$amountMl' } } },
      ]),
      NutritionLog.aggregate([
        { $match: { userId: req.user._id, loggedAt: { $gte: start } } },
        { $group: { _id: null, totalNutritionCalories: { $sum: '$calories' } } },
      ]),
      WorkoutLog.find({ userId: req.user._id }).sort({ createdAt: -1 }).limit(5),
    ]);

    const summary = {
      workoutsCount: workoutAgg[0]?.workoutsCount || 0,
      totalWorkoutMinutes: workoutAgg[0]?.totalWorkoutMinutes || 0,
      totalWorkoutCalories: workoutAgg[0]?.totalWorkoutCalories || 0,
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
