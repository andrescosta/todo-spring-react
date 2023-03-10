import React, { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import App from "./App";
import AuthContextProvider from "./AuthContextProvider";

const root = createRoot(document.getElementById("root"));
root.render(
  // StrictMode is after AuthContextProvider due to: 
  // https://github.com/react-keycloak/react-keycloak/issues/182 
  <AuthContextProvider>
    <StrictMode>
      <App />
    </StrictMode>
  </AuthContextProvider>
);