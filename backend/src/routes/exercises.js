const express = require('express');

const router = express.Router();

const exerciseLibrary = [
  {
    id: 'bench-press',
    name: 'Bench Press',
    muscleGroup: 'Chest',
    difficulty: 'Intermediate',
    defaultDurationMinutes: 20,
  },
  {
    id: 'deadlift',
    name: 'Deadlift',
    muscleGroup: 'Back',
    difficulty: 'Advanced',
    defaultDurationMinutes: 25,
  },
  {
    id: 'squat',
    name: 'Squat',
    muscleGroup: 'Legs',
    difficulty: 'Intermediate',
    defaultDurationMinutes: 20,
  },
  {
    id: 'plank',
    name: 'Plank',
    muscleGroup: 'Core',
    difficulty: 'Beginner',
    defaultDurationMinutes: 10,
  },
  {
    id: 'jump-rope',
    name: 'Jump Rope',
    muscleGroup: 'Cardio',
    difficulty: 'Beginner',
    defaultDurationMinutes: 15,
  },
];

router.get('/', (req, res) => {
  return res.json({ success: true, data: exerciseLibrary });
});

module.exports = router;
