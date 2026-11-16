
import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import keycloak from "./keycloak";
import "./index.css";

keycloak
  .init({
    onLoad: "login-required",
    pkceMethod: "S256",
    checkLoginIframe: false,
  })
  .then((authenticated) => {
    if (!authenticated) {
      keycloak.login();
      return;
    }

    ReactDOM.createRoot(document.getElementById("root")).render(
      <React.StrictMode>
        <App />
      </React.StrictMode>
    );
  })
  .catch((error) => {
    console.error("Keycloak initialization failed:", error);
    document.getElementById("root").innerText =
      "Unable to connect to Keycloak.";
  });