const express = require('express');
const auth = require('../middleware/auth');
const NutritionLog = require('../models/NutritionLog');

const router = express.Router();

router.use(auth);

router.get('/', async (req, res) => {
  try {
    const logs = await NutritionLog.find({ userId: req.user._id }).sort({ loggedAt: -1 }).limit(100);
    return res.json({ success: true, data: logs });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Failed to fetch nutrition logs' });
  }
});

router.get('/today-summary', async (req, res) => {
  try {
    const start = new Date();
    start.setHours(0, 0, 0, 0);

    const aggregate = await NutritionLog.aggregate([
      { $match: { userId: req.user._id, loggedAt: { $gte: start } } },
      {
        $group: {
          _id: null,
          calories: { $sum: '$calories' },
          proteinGrams: { $sum: '$proteinGrams' },
          carbsGrams: { $sum: '$carbsGrams' },
          fatGrams: { $sum: '$fatGrams' },
        },
      },
    ]);

    const summary =
      aggregate[0] ||
      {
        calories: 0,
        proteinGrams: 0,
        carbsGrams: 0,
        fatGrams: 0,
      };

    return res.json({ success: true, data: summary });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Failed to calculate nutrition summary' });
  }
});

router.post('/', async (req, res) => {
  try {
    const { mealName, calories, proteinGrams, carbsGrams, fatGrams, loggedAt } = req.body;

    if (!mealName || typeof mealName !== 'string') {
      return res.status(400).json({ success: false, message: 'mealName is required' });
    }

    if (!Number.isFinite(calories) || calories < 0) {
      return res.status(400).json({ success: false, message: 'calories must be a non-negative number' });
    }

    const log = await NutritionLog.create({
      userId: req.user._id,
      mealName,
      calories,
      proteinGrams: Number.isFinite(proteinGrams) ? proteinGrams : 0,
      carbsGrams: Number.isFinite(carbsGrams) ? carbsGrams : 0,
      fatGrams: Number.isFinite(fatGrams) ? fatGrams : 0,
      loggedAt: loggedAt || new Date(),
    });

    return res.status(201).json({ success: true, data: log });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Failed to create nutrition log' });
  }
});

module.exports = router;
