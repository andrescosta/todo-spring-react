import React, { useState } from "react";
import { useAuthContext } from "./context/AuthContextProvider";
import MainPanel from "./components/MainPanel";
import SideMenu from "./components/SideMenu";
import TopBar from "./components/TopBar";
import LoadingSpinner from "./common/Spinner";

export default function TODOApp() {
  const authContext = useAuthContext();
  const [menuShown, toggleMenu] = useState(false);

  if (authContext.isAuthenticated) {
    return (
      <>
        <TopBar toggleMenu={toggleMenu} menuShown={menuShown} />

        <SideMenu menuShown={menuShown} />

        <MainPanel />
      </>
    );
  } else {
    return <LoadingSpinner />;
  }
}
