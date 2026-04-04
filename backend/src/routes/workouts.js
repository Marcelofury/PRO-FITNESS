const express = require('express');
const WorkoutLog = require('../models/WorkoutLog');

const router = express.Router();

router.get('/', async (req, res) => {
  const workouts = await WorkoutLog.find().sort({ createdAt: -1 }).limit(50);
  res.json({ success: true, data: workouts });
});

router.post('/', async (req, res) => {
  const workout = await WorkoutLog.create(req.body);
  res.status(201).json({ success: true, data: workout });
});

module.exports = router;
