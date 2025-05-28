import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Header from '../layouts/Header';
import HomePage from '../pages/HomePage';

const AppRoutes = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path='/homepage' element={<HomePage/>}/>
        <Route path="/*" element={<Header />} />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRoutes;
