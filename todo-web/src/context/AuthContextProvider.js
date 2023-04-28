import React, { createContext, useState, useContext, useEffect } from "react";
import Keycloak from "keycloak-js";
import axios from "axios";

const keycloak = new Keycloak();

const AuthContext = createContext({
  isAuthenticated: false,
  username: "",
  logout: () => { }
});

export default function AuthContextProvider({ children }) {

  const [isAuthenticated, setAuthenticated] = useState(false);
  const [username, setUsername] = useState("");

  useEffect(() => {
    async function initializeKeycloak() {
      try {
        const isAuthenticatedResponse = await keycloak.init(
          {
            onLoad: "login-required",
          }
        );
        setAuthenticated(isAuthenticatedResponse);

        axios.interceptors.request.use((config) => {
          config.headers["Authorization"] = `Bearer ${keycloak.token}`;
          return config;
        });
        
      } catch (e) {
        setAuthenticated(false);
      }
    }

    initializeKeycloak();
  }, []);

  useEffect(() => {
    async function loadProfile() {
      try {
        const profile = await keycloak.loadUserProfile();
        if (profile.firstName) {
          setUsername(profile.firstName);
        } else if (profile.username) {
          setUsername(profile.username);
        }
      } catch {
      }
    }

    if (isAuthenticated) {
      loadProfile();
    }
  }, [isAuthenticated]);

  const logout = () => {
    keycloak.logout();
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, username, logout }}>
      {children}
    </AuthContext.Provider>)

}

export function useAuthContext() {
  return useContext(AuthContext);
}