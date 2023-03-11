import React, { useState } from "react";
import { useAuthContext } from "./AuthContextProvider";
import MainPanel from "./MainPanel";
import SideMenu from "./SideMenu";
import TopBar from "./TopBar";
import LoadingSpinner from "./Spinner";
import { MetricsProvider } from '@cabify/prom-react';

export default function TODOApp() {
  const authContext = useAuthContext();
  const [menuShown, toggleMenu] = useState(false);
  const normalizePath = (path) => {
    const match = path.match(/\/products\/(\d+)/);
    if (match) {
      return `/products/:id`;
    }
    return path;
  };
  
  if (authContext.isAuthenticated) {
    return (
      <>
        <TopBar toggleMenu={toggleMenu} menuShown={menuShown} />

        <SideMenu menuShown={menuShown} />

        <MainPanel />
      </>);
  }
  else {
    return <LoadingSpinner />
  }
}

