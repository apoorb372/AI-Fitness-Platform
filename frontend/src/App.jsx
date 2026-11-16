
import { useEffect, useState } from "react";
import keycloak from "./keycloak";
import {
  getUserByEmail,
} from "./services/userService";
import {
  getGoals,
  saveGoals,
} from "./services/goalService";
import "./App.css";
import AiRecommendations from "./components/AiRecommendations";
import ProfileSetup from "./components/ProfileSetup";

const API = "http://localhost:8080";

function localDateTime() {
  const date = new Date();
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  return date.toISOString().slice(0, 16);
}

function App() {
  const [user, setUser] = useState(null);
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [savingGoals, setSavingGoals] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [goalError, setGoalError] = useState("");
  const [goalSuccess, setGoalSuccess] = useState("");
  const [profileRequired, setProfileRequired] = useState(false);

  const [goals, setGoals] = useState({
    dailyCaloriesTarget: "2200",
    weeklyWorkoutTarget: "5",
  });

  const [form, setForm] = useState({
    activityType: "RUNNING",
    duration: "",
    distance: "",
    steps: "",
    caloriesBurned: "",
    activityDate: localDateTime(),
  });

  const username =
    user?.name ||
    keycloak.tokenParsed?.preferred_username ||
    "Fitness User";

  async function apiRequest(path, options = {}) {
    await keycloak.updateToken(30);

    const response = await fetch(`${API}${path}`, {
      ...options,
      headers: {
        Authorization: `Bearer ${keycloak.token}`,
        ...(options.body && {
          "Content-Type": "application/json",
        }),
        ...options.headers,
      },
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || `Request failed: ${response.status}`);
    }

    if (response.status === 204) {
      return null;
    }

    return response.json();
  }

  async function loadActivities(userId) {
    if (!userId) return;

    setLoading(true);
    setError("");

    try {
      const data = await apiRequest(
        `/activity-service/api/activities/user/${userId}`
      );

      setActivities(Array.isArray(data) ? data : []);
    } catch (err) {
      setError("Unable to load activities. " + err.message);
    } finally {
      setLoading(false);
    }
  }

  async function loadUserData(userData) {
    setUser(userData);
    setProfileRequired(false);
    setLoading(true);
    setError("");
    setGoalError("");

    const results = await Promise.allSettled([
      apiRequest(
        `/activity-service/api/activities/user/${userData.id}`
      ),
      getGoals(userData.id),
    ]);

    if (results[0].status === "fulfilled") {
      const data = results[0].value;
      setActivities(Array.isArray(data) ? data : []);
    } else {
      setError(
        "Unable to load activities. " +
          results[0].reason.message
      );
    }

    if (results[1].status === "fulfilled") {
      const data = results[1].value;

      if (data) {
        setGoals({
          dailyCaloriesTarget: String(data.dailyCaloriesTarget),
          weeklyWorkoutTarget: String(data.weeklyWorkoutTarget),
        });
      }
    } else {
      setGoalError(
        "Unable to load goals. " +
          results[1].reason.message
      );
    }

    setLoading(false);
  }

  useEffect(() => {
    let active = true;

    async function initializeDashboard() {
      try {
        setLoading(true);
        setError("");
        setGoalError("");

        const email = keycloak.tokenParsed?.email;

        if (!email) {
          throw new Error(
            "Email not found in Keycloak login details."
          );
        }

        const userData = await getUserByEmail(email);

        if (!active) return;

        setUser(userData);

        const results = await Promise.allSettled([
          apiRequest(
            `/activity-service/api/activities/user/${userData.id}`
          ),
          getGoals(userData.id),
        ]);

        if (!active) return;

        if (results[0].status === "fulfilled") {
          const data = results[0].value;
          setActivities(Array.isArray(data) ? data : []);
        } else {
          setError(
            "Unable to load activities. " +
              results[0].reason.message
          );
        }

        if (results[1].status === "fulfilled") {
          const data = results[1].value;

          if (data) {
            setGoals({
              dailyCaloriesTarget: String(
                data.dailyCaloriesTarget
              ),
              weeklyWorkoutTarget: String(
                data.weeklyWorkoutTarget
              ),
            });
          }
        } else {
          setGoalError(
            "Unable to load goals. " +
              results[1].reason.message
          );
        }
      } catch (err) {
        if (active) {
          if (err.status === 404) {
            setProfileRequired(true);
          } else {
            setError(
              "Unable to load dashboard. " + err.message
            );
          }
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    initializeDashboard();

    return () => {
      active = false;
    };
  }, []);

  function handleChange(event) {
    const { name, value } = event.target;

    setForm(previous => ({
      ...previous,
      [name]: value,
    }));
  }

  function handleGoalChange(event) {
    const { name, value } = event.target;

    setGoals(previous => ({
      ...previous,
      [name]: value,
    }));
  }

  async function handleGoalSubmit(event) {
    event.preventDefault();

    setSavingGoals(true);
    setGoalError("");
    setGoalSuccess("");

    try {
      if (!user?.id) {
        throw new Error("User profile is not loaded.");
      }

      const payload = {
        dailyCaloriesTarget: Number(goals.dailyCaloriesTarget),
        weeklyWorkoutTarget: Number(goals.weeklyWorkoutTarget),
      };

      if (
        !Number.isInteger(payload.dailyCaloriesTarget) ||
        payload.dailyCaloriesTarget < 100 ||
        payload.dailyCaloriesTarget > 10000
      ) {
        throw new Error(
          "Daily calories must be between 100 and 10000."
        );
      }

      if (
        !Number.isInteger(payload.weeklyWorkoutTarget) ||
        payload.weeklyWorkoutTarget < 1 ||
        payload.weeklyWorkoutTarget > 21
      ) {
        throw new Error(
          "Weekly workouts must be between 1 and 21."
        );
      }

      const saved = await saveGoals(user.id, payload);

      setGoals({
        dailyCaloriesTarget: String(saved.dailyCaloriesTarget),
        weeklyWorkoutTarget: String(saved.weeklyWorkoutTarget),
      });

      setGoalSuccess("Your fitness goals have been saved.");
    } catch (err) {
      setGoalError("Unable to save goals. " + err.message);
    } finally {
      setSavingGoals(false);
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();

    setSaving(true);
    setError("");
    setSuccess("");

    try {
      if (!user?.id) {
        throw new Error("User profile is not loaded.");
      }

      const payload = {
        userId: Number(user.id),
        activityType: form.activityType,
        duration: Number(form.duration),
        distance:
          form.distance === "" ? null : Number(form.distance),
        steps: form.steps === "" ? null : Number(form.steps),
        caloriesBurned:
          form.caloriesBurned === ""
            ? null
            : Number(form.caloriesBurned),
        activityDate: form.activityDate,
      };

      await apiRequest("/activity-service/api/activities", {
        method: "POST",
        body: JSON.stringify(payload),
      });

      setSuccess("Activity added successfully.");

      setForm(previous => ({
        ...previous,
        duration: "",
        distance: "",
        steps: "",
        caloriesBurned: "",
        activityDate: localDateTime(),
      }));

      await loadActivities(user.id);
    } catch (err) {
      setError("Unable to save activity. " + err.message);
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(activityId) {
    const confirmed = window.confirm(
      "Are you sure you want to delete this activity?"
    );

    if (!confirmed) return;

    setDeletingId(activityId);
    setError("");
    setSuccess("");

    try {
      await apiRequest(
        `/activity-service/api/activities/${encodeURIComponent(activityId)}`,
        { method: "DELETE" }
      );

      setActivities(previous =>
        previous.filter(activity => activity.id !== activityId)
      );

      setSuccess("Activity deleted successfully.");
    } catch (err) {
      setError("Unable to delete activity. " + err.message);
    } finally {
      setDeletingId(null);
    }
  }

  const totalCalories = activities.reduce(
    (sum, item) => sum + (Number(item.caloriesBurned) || 0),
    0
  );

  const totalMinutes = activities.reduce(
    (sum, item) => sum + (Number(item.duration) || 0),
    0
  );

  const totalDistance = activities.reduce(
    (sum, item) => sum + (Number(item.distance) || 0),
    0
  );

  const weeklyWorkoutCount = activities.filter(activity => {
    if (!activity.activityDate) return false;

    const date = new Date(activity.activityDate);
    const now = new Date();

    const startOfWeek = new Date(now);
    const day = (now.getDay() + 6) % 7;

    startOfWeek.setDate(now.getDate() - day);
    startOfWeek.setHours(0, 0, 0, 0);

    const endOfWeek = new Date(startOfWeek);
    endOfWeek.setDate(startOfWeek.getDate() + 7);

    return date >= startOfWeek && date < endOfWeek;
  }).length;

  const weeklyTarget = Number(goals.weeklyWorkoutTarget) || 0;

  const weeklyProgress =
    weeklyTarget > 0
      ? Math.min((weeklyWorkoutCount / weeklyTarget) * 100, 100)
      : 0;

  if (profileRequired) {
    return (
      <ProfileSetup
        onComplete={loadUserData}
      />
    );
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">AF</div>
          <div>
            <strong>AI FITNESS</strong>
            <span>HEALTH PLATFORM</span>
          </div>
        </div>

        <nav className="navigation">
          <div className="nav-label">MENU</div>
          <a className="nav-item active" href="#dashboard">Dashboard</a>
          <a className="nav-item" href="#activities">Activities</a>
          <a className="nav-item" href="#goals">Fitness goals</a>
          <a className="nav-item" href="#add-activity">Add activity</a>
        </nav>

        <div className="sidebar-bottom">
          <div className="user-avatar">
            {username.charAt(0).toUpperCase()}
          </div>

          <div className="user-info">
            <strong>{username}</strong>
            <span>Fitness member</span>
          </div>

          <button
            className="logout-button"
            title="Logout"
            onClick={() =>
              keycloak.logout({
                redirectUri: window.location.origin,
              })
            }
          >
            Logout
          </button>
        </div>
      </aside>

      <main className="main-content" id="dashboard">
        <header className="topbar">
          <div>
            <span className="eyebrow">
              YOUR PERSONAL FITNESS SPACE
            </span>
            <h1>Fitness Dashboard</h1>
          </div>

          <div className="profile-pill">
            <span className="online-dot" />
            {username}
          </div>
        </header>

        <section className="welcome-panel">
          <div>
            <span className="welcome-label">WELCOME BACK</span>
            <h2>Let's keep moving, {username}!</h2>
            <p>
              Track your workouts and keep an eye on your fitness progress.
            </p>
          </div>
          <div className="welcome-symbol">✦</div>
        </section>

        <section className="stats-grid">
          <div className="stat-card">
            <div className="stat-heading">
              <span>Total activities</span>
              <span className="stat-icon purple">◷</span>
            </div>
            <strong className="stat-value">{activities.length}</strong>
            <span className="stat-note">Recorded workouts</span>
          </div>

          <div className="stat-card">
            <div className="stat-heading">
              <span>Calories burned</span>
              <span className="stat-icon orange">♨</span>
            </div>
            <strong className="stat-value">
              {totalCalories.toLocaleString("en-IN", {
                maximumFractionDigits: 1,
              })}
            </strong>
            <span className="stat-note">Total recorded kcal</span>
          </div>

          <div className="stat-card">
            <div className="stat-heading">
              <span>Workout duration</span>
              <span className="stat-icon blue">◷</span>
            </div>
            <strong className="stat-value">{totalMinutes}</strong>
            <span className="stat-note">Total minutes</span>
          </div>

          <div className="stat-card">
            <div className="stat-heading">
              <span>Distance</span>
              <span className="stat-icon green">⌁</span>
            </div>
            <strong className="stat-value">
              {totalDistance.toFixed(1)}
            </strong>
            <span className="stat-note">Total kilometres</span>
          </div>
        </section>

        <section
          className="panel"
          id="goals"
          style={{ marginBottom: "24px" }}
        >
          <div className="panel-heading">
            <div>
              <h2>Fitness goals</h2>
              <p>Set your personal fitness targets</p>
            </div>
            <span className="form-plus">◎</span>
          </div>

          <form onSubmit={handleGoalSubmit} className="activity-form">
            <div className="form-two-columns">
              <label>
                Daily calorie target (kcal)
                <input
                  type="number"
                  name="dailyCaloriesTarget"
                  min="100"
                  max="10000"
                  step="1"
                  value={goals.dailyCaloriesTarget}
                  onChange={handleGoalChange}
                  required
                />
              </label>

              <label>
                Weekly workout target
                <input
                  type="number"
                  name="weeklyWorkoutTarget"
                  min="1"
                  max="21"
                  step="1"
                  value={goals.weeklyWorkoutTarget}
                  onChange={handleGoalChange}
                  required
                />
              </label>
            </div>

            <div
              style={{
                padding: "16px",
                borderRadius: "12px",
                background: "var(--card-background, #f4f3fb)",
                marginTop: "8px",
              }}
            >
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  gap: "12px",
                  marginBottom: "10px",
                }}
              >
                <strong>Weekly workout progress</strong>
                <strong>
                  {weeklyWorkoutCount} / {weeklyTarget}
                </strong>
              </div>

              <div
                style={{
                  height: "10px",
                  borderRadius: "20px",
                  background: "#dedbea",
                  overflow: "hidden",
                }}
              >
                <div
                  style={{
                    width: `${weeklyProgress}%`,
                    height: "100%",
                    borderRadius: "20px",
                    background: "#7864d8",
                    transition: "width 0.3s ease",
                  }}
                />
              </div>

              <p style={{ marginTop: "8px", fontSize: "13px" }}>
                {weeklyWorkoutCount >= weeklyTarget && weeklyTarget > 0
                  ? "Your weekly workout target has been reached."
                  : `${Math.max(weeklyTarget - weeklyWorkoutCount, 0)} workouts remaining this week.`}
              </p>
            </div>

            {goalError && (
              <div className="message error">{goalError}</div>
            )}

            {goalSuccess && (
              <div className="message success">{goalSuccess}</div>
            )}

            <button
              className="submit-button"
              type="submit"
              disabled={savingGoals || !user?.id}
            >
              {savingGoals ? "Saving goals..." : "Save fitness goals →"}
            </button>
          </form>
        </section>

        <AiRecommendations />

        <section className="content-grid">
          <div className="panel activity-panel" id="activities">
            <div className="panel-heading">
              <div>
                <h2>Recent activities</h2>
                <p>Your recorded fitness sessions</p>
              </div>

              <button
                className="refresh-button"
                onClick={() => loadActivities(user?.id)}
                disabled={loading || !user?.id}
              >
                ↻ Refresh
              </button>
            </div>

            {error && (
              <div className="message error">{error}</div>
            )}

            {success && (
              <div className="message success">{success}</div>
            )}

            {loading ? (
              <div className="empty-state">
                Loading activities...
              </div>
            ) : activities.length === 0 ? (
              <div className="empty-state">
                <div className="empty-icon">◷</div>
                <strong>No activities yet</strong>
                <p>Add your first workout to see it here.</p>
              </div>
            ) : (
              <div className="activity-list">
                {[...activities]
                  .sort(
                    (a, b) =>
                      new Date(b.activityDate) -
                      new Date(a.activityDate)
                  )
                  .map(activity => (
                    <div className="activity-row" key={activity.id}>
                      <div className="activity-symbol">
                        {activity.activityType === "RUNNING"
                          ? "🏃"
                          : activity.activityType === "CYCLING"
                            ? "🚴"
                            : activity.activityType === "WALKING"
                              ? "🚶"
                              : "💪"}
                      </div>

                      <div className="activity-details">
                        <strong>{activity.activityType}</strong>
                        <span>
                          {activity.activityDate
                            ? new Date(
                                activity.activityDate
                              ).toLocaleString()
                            : "Date unavailable"}
                        </span>
                      </div>

                      <div className="activity-metrics">
                        <strong>{activity.duration} min</strong>
                        <span>
                          {activity.caloriesBurned ?? 0} kcal
                        </span>
                      </div>

                      <button
                        className="delete-button"
                        title="Delete activity"
                        aria-label={`Delete ${activity.activityType} activity`}
                        disabled={deletingId === activity.id}
                        onClick={() => handleDelete(activity.id)}
                      >
                        {deletingId === activity.id
                          ? "Deleting..."
                          : "Delete"}
                      </button>
                    </div>
                  ))}
              </div>
            )}
          </div>

          <div className="panel form-panel" id="add-activity">
            <div className="panel-heading">
              <div>
                <h2>Add activity</h2>
                <p>Record a workout session</p>
              </div>
              <span className="form-plus">＋</span>
            </div>

            <form onSubmit={handleSubmit} className="activity-form">
              <label>
                User
                <input
                  type="text"
                  value={user?.name || "Loading profile..."}
                  readOnly
                />
              </label>

              <label>
                Activity type
                <select
                  name="activityType"
                  value={form.activityType}
                  onChange={handleChange}
                  required
                >
                  <option value="RUNNING">Running</option>
                  <option value="WALKING">Walking</option>
                  <option value="CYCLING">Cycling</option>
                  <option value="GYM">Gym</option>
                  <option value="YOGA">Yoga</option>
                  <option value="SWIMMING">Swimming</option>
                </select>
              </label>

              <div className="form-two-columns">
                <label>
                  Duration (min)
                  <input
                    name="duration"
                    type="number"
                    min="1"
                    value={form.duration}
                    onChange={handleChange}
                    placeholder="45"
                    required
                  />
                </label>

                <label>
                  Distance (km)
                  <input
                    name="distance"
                    type="number"
                    min="0"
                    step="0.1"
                    value={form.distance}
                    onChange={handleChange}
                    placeholder="5.2"
                  />
                </label>
              </div>

              <div className="form-two-columns">
                <label>
                  Steps
                  <input
                    name="steps"
                    type="number"
                    min="0"
                    value={form.steps}
                    onChange={handleChange}
                    placeholder="6200"
                  />
                </label>

                <label>
                  Calories (kcal)
                  <input
                    name="caloriesBurned"
                    type="number"
                    min="0"
                    step="0.1"
                    value={form.caloriesBurned}
                    onChange={handleChange}
                    placeholder="420"
                  />
                </label>
              </div>

              <label>
                Activity date
                <input
                  name="activityDate"
                  type="datetime-local"
                  value={form.activityDate}
                  onChange={handleChange}
                  required
                />
              </label>

              <button
                className="submit-button"
                type="submit"
                disabled={saving || !user?.id}
              >
                {saving ? "Saving..." : "Save activity →"}
              </button>
            </form>
          </div>
        </section>

        <footer className="footer">
          AI Fitness Platform · Your progress, in one place
        </footer>
      </main>
    </div>
  );
}

export default App;
