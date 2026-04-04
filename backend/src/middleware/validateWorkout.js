function validateWorkoutPayload(req, res, next) {
  const { userId, workoutName, durationMinutes, caloriesBurned } = req.body;

  if (!userId || typeof userId !== 'string' || userId.trim().length === 0) {
    return res.status(400).json({ success: false, message: 'userId is required' });
  }

  if (!workoutName || typeof workoutName !== 'string' || workoutName.trim().length === 0) {
    return res.status(400).json({ success: false, message: 'workoutName is required' });
  }

  if (!Number.isFinite(durationMinutes) || durationMinutes <= 0) {
    return res.status(400).json({ success: false, message: 'durationMinutes must be a positive number' });
  }

  if (caloriesBurned !== undefined && (!Number.isFinite(caloriesBurned) || caloriesBurned < 0)) {
    return res.status(400).json({ success: false, message: 'caloriesBurned must be a non-negative number' });
  }

  return next();
}

module.exports = validateWorkoutPayload;