import React from 'react'
import { BrowserRouter,Route,Routes } from 'react-router-dom'
import UserRegister from '../pages/UserRegister'
import UserLogin from '../pages/Userlogin'


const AppRoutes = () => {
  return (
    <div>
    <BrowserRouter>
    <Routes>
    <Route path='/' element={<UserRegister/>}/>
    <Route path='/login' element={<UserLogin/>}/>
    </Routes>
    </BrowserRouter>
    </div>
  )
}

export default AppRoutes;