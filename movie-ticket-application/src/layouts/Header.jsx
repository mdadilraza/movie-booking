import React from 'react';
import styles from '../assets/styles/homepage.module.css'
import PersonIcon from '@mui/icons-material/Person';
import {
  Button,
  Dialog,
  DialogContent,
  TextField,
  InputAdornment,
  IconButton
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { useLocation, useNavigate, Routes, Route, Link } from 'react-router-dom';
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
        backgroundColor: '#121f2f',
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
        <SearchIcon sx={{ color: 'white' }} />
      </InputAdornment>
    ),
    style: { color: 'white' }, 
  }}
  InputLabelProps={{
    style: { color: 'white' }, 
  }}
  sx={{
    '& .MuiOutlinedInput-root': {
      '& fieldset': {
        borderColor: 'white',
        height:'10vh',
        marginTop:'3px'
      },
      '&:hover fieldset': {
        borderColor: 'white',
      },
      '&.Mui-focused fieldset': {
        borderColor: 'white',
      },
    },
  }}
/>


     <Link className={styles.header}>MOVIES</Link>
     <Link className={styles.header}>EVENTS</Link>
     <Link className={styles.header}>PLAYS</Link>
     <Link className={styles.header}>SPORTS</Link>
      
      <IconButton onClick={()=>navigate('/register')} sx={{backgroundColor:'#1c2b3a'}}>
      <PersonIcon sx={{color:'white',backgroundColor:'#1c2b3a'}}/>
      </IconButton>
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
