const express = require('express');
const mongoose = require('mongoose');
const WorkoutLog = require('../models/WorkoutLog');
const validateWorkoutPayload = require('../middleware/validateWorkout');

const router = express.Router();

router.get('/', async (req, res) => {
  try {
    const workouts = await WorkoutLog.find().sort({ createdAt: -1 }).limit(50);
    res.json({ success: true, data: workouts });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to fetch workouts' });
  }
});

router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;

    if (!mongoose.Types.ObjectId.isValid(id)) {
      return res.status(400).json({ success: false, message: 'Invalid workout id' });
    }

    const workout = await WorkoutLog.findById(id);

    if (!workout) {
      return res.status(404).json({ success: false, message: 'Workout not found' });
    }

    return res.json({ success: true, data: workout });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Failed to fetch workout' });
  }
});

router.post('/', validateWorkoutPayload, async (req, res) => {
  try {
    const workout = await WorkoutLog.create(req.body);
    res.status(201).json({ success: true, data: workout });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to create workout' });
  }
});

router.put('/:id', validateWorkoutPayload, async (req, res) => {
  try {
    const { id } = req.params;

    if (!mongoose.Types.ObjectId.isValid(id)) {
      return res.status(400).json({ success: false, message: 'Invalid workout id' });
    }

    const updatedWorkout = await WorkoutLog.findByIdAndUpdate(id, req.body, {
      new: true,
      runValidators: true,
    });

    if (!updatedWorkout) {
      return res.status(404).json({ success: false, message: 'Workout not found' });
    }

    return res.json({ success: true, data: updatedWorkout });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Failed to update workout' });
  }
});

router.delete('/:id', async (req, res) => {
  try {
    const { id } = req.params;

    if (!mongoose.Types.ObjectId.isValid(id)) {
      return res.status(400).json({ success: false, message: 'Invalid workout id' });
    }

    const deletedWorkout = await WorkoutLog.findByIdAndDelete(id);

    if (!deletedWorkout) {
      return res.status(404).json({ success: false, message: 'Workout not found' });
    }

    return res.json({ success: true, message: 'Workout deleted' });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Failed to delete workout' });
  }
});

module.exports = router;
