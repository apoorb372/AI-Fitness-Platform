
import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
  url: "http://localhost:9090",
  realm: "ai-fitness",
  clientId: "ai-fitness-frontend",
});

export default keycloak;