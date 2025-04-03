import './App.css'
import {Route, BrowserRouter as Router} from "react-router-dom";
import {Routes} from "react-router";
import LoginPage from "./Pages/LoginPage/LoginPage.tsx";
import HomePage from "@/Pages/HomePage/HomePage.tsx";
import Dashboard from "@/Pages/Dashboard/Dashboard.tsx";
import ConsentForm from "@/Pages/ConsentPage/ConsentForm.tsx";
import ErrorPage from "@/Pages/ErrorPage/ErrorPage.tsx";
import ClientRegistration from "@/Pages/ClientRegistrationPage/ClientRegistration.tsx";




function App() {


  return (
      <Router>
          <Routes>
              <Route path={"/login"} element={<LoginPage />}/>
              <Route path={"/"} element={<HomePage/>}/>
              <Route path={"/dashboard"} element={<Dashboard/>}/>
              <Route path="/oauth/user-consent" element={<ConsentForm/>}/>
              <Route path="/error" element={<ErrorPage/>}/>
              <Route path={"/client-registration"} element={<ClientRegistration/>}></Route>
          </Routes>
      </Router>
  )
}

export default App
