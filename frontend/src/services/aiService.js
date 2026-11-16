
import keycloak from "../keycloak";

const API_URL = "http://localhost:8080";

export async function generateRecommendations(data) {
  await keycloak.updateToken(30);

  const response = await fetch(
    `${API_URL}/ai-service/api/ai/recommendations`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${keycloak.token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    }
  );

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(
      errorText || `AI recommendation failed: ${response.status}`
    );
  }

  return response.json();
}