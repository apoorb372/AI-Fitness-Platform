
import { useState } from "react";
import keycloak from "../keycloak";
import { createUserProfile } from "../services/userService";

export default function ProfileSetup({ onComplete }) {
  const account = keycloak.tokenParsed || {};

  const [form, setForm] = useState({
    name: account.name || account.preferred_username || "",
    email: account.email || "",
    gender: "",
    age: "",
    height: "",
    weight: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  function handleChange(event) {
    const { name, value } = event.target;

    setForm(previous => ({
      ...previous,
      [name]: value,
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();

    setLoading(true);
    setError("");

    try {
      if (!form.email) {
        throw new Error(
          "Your Keycloak account must have a valid email address."
        );
      }

      const profile = {
        name: form.name.trim(),
        email: form.email.trim().toLowerCase(),
        gender: form.gender,
        age: Number(form.age),
        height: Number(form.height),
        weight: Number(form.weight),
      };

      if (
        !profile.name ||
        !profile.gender ||
        !Number.isInteger(profile.age) ||
        profile.age <= 0 ||
        !Number.isFinite(profile.height) ||
        profile.height <= 0 ||
        !Number.isFinite(profile.weight) ||
        profile.weight <= 0
      ) {
        throw new Error("Please enter valid information in every field.");
      }

      const savedUser = await createUserProfile(profile);

      onComplete(savedUser);
    } catch (err) {
      setError(err.message || "Unable to save your profile.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="app-shell">
      <main className="main-content" style={{ maxWidth: "760px", margin: "auto" }}>
        <section className="panel" style={{ marginTop: "70px" }}>
          <div className="panel-heading">
            <div>
              <span className="eyebrow">WELCOME TO AI FITNESS</span>
              <h2>Complete your fitness profile</h2>
              <p>
                We need a few details to personalize your dashboard
                and fitness recommendations.
              </p>
            </div>
          </div>

          <form className="activity-form" onSubmit={handleSubmit}>
            <label>
              Full name
              <input
                name="name"
                type="text"
                value={form.name}
                onChange={handleChange}
                required
              />
            </label>

            <label>
              Email address
              <input
                type="email"
                value={form.email}
                readOnly
              />
            </label>

            <label>
              Gender
              <select
                name="gender"
                value={form.gender}
                onChange={handleChange}
                required
              >
                <option value="">Select gender</option>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
                <option value="Other">Other</option>
                <option value="Prefer not to say">Prefer not to say</option>
              </select>
            </label>

            <div className="form-two-columns">
              <label>
                Age (years)
                <input
                  name="age"
                  type="number"
                  min="1"
                  max="120"
                  value={form.age}
                  onChange={handleChange}
                  required
                />
              </label>

              <label>
                Height (cm)
                <input
                  name="height"
                  type="number"
                  min="1"
                  step="0.1"
                  value={form.height}
                  onChange={handleChange}
                  placeholder="175"
                  required
                />
              </label>
            </div>

            <label>
              Weight (kg)
              <input
                name="weight"
                type="number"
                min="1"
                step="0.1"
                value={form.weight}
                onChange={handleChange}
                placeholder="70"
                required
              />
            </label>

            {error && (
              <div className="message error">
                {error}
              </div>
            )}

            <button
              type="submit"
              className="submit-button"
              disabled={loading}
            >
              {loading ? "Saving profile..." : "Save profile and continue"}
            </button>
          </form>
        </section>
      </main>
    </div>
  );
}
