import React from 'react';
import {
  Button,
  Dialog,
  DialogContent,
  TextField,
  InputAdornment
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { useLocation, useNavigate, Routes, Route } from 'react-router-dom';
import UserLogin from '../pages/Userlogin';
import UserRegister from '../pages/UserRegister';

const Header = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const isLogin = location.pathname === '/login';
  const isRegister = location.pathname === '/register';
  const dialogOpen = isLogin || isRegister;

  const handleClose = () => {
    navigate('/'); 
  };

  return (
    <div
      style={{
        display: 'flex',
        height: '10vh',
        backgroundColor: 'white',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '10px',
      }}
    >
      <img src="/logo-movie.png" alt="logo" style={{ width: '150px', height: '10vh' }} />

      <TextField
        placeholder="Search..."
        variant="outlined"
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <SearchIcon />
            </InputAdornment>
          ),
        }}
      />

     
      <Button onClick={() => navigate('/register')}>SignIn</Button>

      <Dialog open={dialogOpen} onClose={handleClose} maxWidth="sm" >
        <DialogContent>
          <Routes>
            <Route path="/login" element={<UserLogin />} />
            <Route path="/register" element={<UserRegister />} />
          </Routes>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default Header;
