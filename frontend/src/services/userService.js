
import keycloak from "../keycloak";

const API_URL = "http://localhost:8080";

async function getAuthHeaders() {
  await keycloak.updateToken(30);

  return {
    Authorization: `Bearer ${keycloak.token}`,
    "Content-Type": "application/json",
  };
}

export async function getUserByEmail(email) {
  const headers = await getAuthHeaders();

  const response = await fetch(
    `${API_URL}/user-service/api/users/email/${encodeURIComponent(email)}`,
    { headers }
  );

  if (!response.ok) {
    const error = new Error(
      response.status === 404
        ? "Fitness profile not found"
        : `Unable to fetch user: ${response.status}`
    );

    error.status = response.status;
    throw error;
  }

  return response.json();
}

export async function createUserProfile(profile) {
  const headers = await getAuthHeaders();

  const response = await fetch(
    `${API_URL}/user-service/api/users/register`,
    {
      method: "POST",
      headers,
      body: JSON.stringify(profile),
    }
  );

  if (!response.ok) {
    const message = await response.text();

    throw new Error(
      message || `Unable to create profile: ${response.status}`
    );
  }

  return response.json();
}
