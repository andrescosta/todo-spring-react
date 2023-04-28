import { ActivitiesProvider } from "../context/ActivitiesContext"
import { Footer, Header } from "../components/CommonUI"
import ToolBar from "../components/ToolBar"
import ActivityPanel from "../components/ActivityPanel"
import React from "react"
export default function MainPanel() {
  return (<>
    <ActivitiesProvider>
      <div className="w3-main" style={{ marginLeft: 50 + 'px', marginTop: 43 + 'px' }}>
        <Header />
        <p> </p>
        <p> </p>
        <ToolBar />
        <div>
        </div>
        <ActivityPanel />
        <Footer />

      </div>
    </ActivitiesProvider>
  </>)
}
