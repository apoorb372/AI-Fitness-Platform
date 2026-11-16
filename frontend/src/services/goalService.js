
import keycloak from "../keycloak";

const API_URL = "http://localhost:8080";

async function getHeaders() {
  await keycloak.updateToken(30);

  return {
    Authorization: `Bearer ${keycloak.token}`,
    "Content-Type": "application/json",
  };
}

export async function getGoals(userId) {
  const response = await fetch(
    `${API_URL}/user-service/api/goals/user/${userId}`,
    {
      headers: await getHeaders(),
    }
  );

  if (response.status === 404) {
    return null;
  }

  if (!response.ok) {
    throw new Error(`Unable to fetch goals: ${response.status}`);
  }

  return response.json();
}

export async function saveGoals(userId, goals) {
  const response = await fetch(
    `${API_URL}/user-service/api/goals/user/${userId}`,
    {
      method: "PUT",
      headers: await getHeaders(),
      body: JSON.stringify(goals),
    }
  );

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Unable to save goals: ${response.status}`);
  }

  return response.json();
}