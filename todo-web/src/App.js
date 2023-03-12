import React, { useState } from "react";
import { useAuthContext } from "./AuthContextProvider";
import MainPanel from "./MainPanel";
import SideMenu from "./SideMenu";
import TopBar from "./TopBar";
import LoadingSpinner from "./Spinner";

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
