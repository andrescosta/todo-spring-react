import { ActivitiesProvider } from "./ActivitiesContext"
import { Footer, Header } from "./CommonUI"
import ToolBar from "./ToolBar"
import ActivityPanel from "./ActivityPanel"
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
