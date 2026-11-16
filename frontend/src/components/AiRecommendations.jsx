
import { useState } from "react";
import keycloak from "../keycloak";
import { getUserByEmail } from "../services/userService";
import { getGoals } from "../services/goalService";
import { generateRecommendations } from "../services/aiService";
import "./AiRecommendations.css";

const API_URL = "http://localhost:8080";

export default function AiRecommendations() {
  const [fitnessGoal, setFitnessGoal] = useState("Improve overall fitness");
  const [recommendation, setRecommendation] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function getActivities(userId) {
    await keycloak.updateToken(30);

    const response = await fetch(
      `${API_URL}/activity-service/api/activities/user/${userId}`,
      {
        headers: {
          Authorization: `Bearer ${keycloak.token}`,
        },
      }
    );

    if (!response.ok) {
      throw new Error(`Unable to fetch activities: ${response.status}`);
    }

    return response.json();
  }

  async function handleGenerate() {
    setLoading(true);
    setError("");
    setRecommendation("");

    try {
      const email = keycloak.tokenParsed?.email;

      if (!email) {
        throw new Error("Your login account has no email address.");
      }

      const user = await getUserByEmail(email);

      if (!user?.id) {
        throw new Error("User profile could not be found.");
      }

      const [goals, activities] = await Promise.all([
        getGoals(user.id),
        getActivities(user.id),
      ]);

      const request = {
        userId: user.id,
        age: Number(user.age),
        gender: user.gender,
        height: Number(user.height),
        weight: Number(user.weight),
        fitnessGoal,
        dailyCaloriesTarget: Number(
          goals?.dailyCaloriesTarget ?? 2200
        ),
        weeklyWorkoutTarget: Number(
          goals?.weeklyWorkoutTarget ?? 5
        ),
        activities: activities.slice(0, 30).map((activity) => ({
          activityType: activity.activityType,
          duration: activity.duration,
          distance: activity.distance,
          steps: activity.steps,
          caloriesBurned: activity.caloriesBurned,
          activityDate: activity.activityDate,
        })),
      };

      if (
        !Number.isFinite(request.age) ||
        !request.gender ||
        !Number.isFinite(request.height) ||
        !Number.isFinite(request.weight)
      ) {
        throw new Error(
          "Your profile is missing age, gender, height or weight. Please update your profile first."
        );
      }

      const result = await generateRecommendations(request);
      setRecommendation(result.recommendation);
    } catch (err) {
      setError(
        err.message || "Unable to generate recommendations."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="ai-recommendations">
      <div className="ai-heading">
        <div>
          <span className="ai-eyebrow">PERSONAL FITNESS ASSISTANT</span>
          <h2>AI Fitness Recommendations</h2>
          <p>
            Get workout and nutrition suggestions based on your
            profile, goals and recent activities.
          </p>
        </div>
        <div className="ai-icon" aria-hidden="true">
          ✦
        </div>
      </div>

      <div className="ai-controls">
        <label htmlFor="fitness-goal">Your fitness goal</label>
        <select
          id="fitness-goal"
          value={fitnessGoal}
          onChange={(event) => setFitnessGoal(event.target.value)}
          disabled={loading}
        >
          <option value="Improve overall fitness">
            Improve overall fitness
          </option>
          <option value="Improve stamina">
            Improve stamina
          </option>
          <option value="Build muscle">
            Build muscle
          </option>
          <option value="Improve cardiovascular endurance">
            Improve cardiovascular endurance
          </option>
          <option value="Improve flexibility">
            Improve flexibility
          </option>
          <option value="Maintain a healthy weight">
            Maintain a healthy weight
          </option>
        </select>

        <button
          type="button"
          className="ai-generate-button"
          onClick={handleGenerate}
          disabled={loading}
        >
          {loading ? "Generating..." : "✦ Generate Recommendations"}
        </button>
      </div>

      {loading && (
        <div className="ai-loading" role="status">
          <span className="ai-spinner" />
          <p>Analyzing your fitness data and generating suggestions...</p>
        </div>
      )}

      {error && (
        <div className="ai-error" role="alert">
          {error}
        </div>
      )}

      {recommendation && (
        <div className="ai-result">
          <div className="ai-result-heading">
            <h3>Your personalized recommendations</h3>
            <button
              type="button"
              className="ai-copy-button"
              onClick={() =>
                navigator.clipboard.writeText(recommendation)
              }
            >
              Copy
            </button>
          </div>
          <div className="ai-result-text">
            {recommendation.split("\n").map((line, index) => (
              <p key={index}>{line || "\u00A0"}</p>
            ))}
          </div>
          <small>
            General fitness information only; not a substitute
            for professional medical advice.
          </small>
        </div>
      )}
    </section>
  );
}