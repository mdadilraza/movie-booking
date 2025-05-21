import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Header from '../layouts/Header';

const AppRoutes = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/*" element={<Header />} />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRoutes;
