import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { register } from '../slice/UserSlice';
import { Button, Card, TextField } from '@mui/material';

const UserRegister = () => {
  const cardStyles = {
    width: '400px',
    height: 'auto',
    display: 'flex',
    flexDirection: 'column',
    padding: '30px',
 
  };

  const dispatch = useDispatch();
  const { loading, error } = useSelector((state) => state.auth);
  const [formError, setFormError] = useState("");
  const [formData, setFormData] = useState({
    username: "",
    password: "",
    email: "",
    fullName: "",
    phoneNumber: "",
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    if (!formData.username || !formData.password || !formData.email || !formData.phoneNumber || !formData.fullName) {
      setFormError("All fields are required!");
      return;
    }

    setFormError("");
    dispatch(register(formData))
      .unwrap()
      .then(() => {
        console.log("Registration successful", formData);
      })
      .catch((error) => {
        setFormError(error.message || "Registration failed");
      });
  };

  return (
    <form onSubmit={handleSubmit}>
      <Card sx={cardStyles}>
        <h2>Sign Up</h2>
        {formError && <p style={{ color: 'red' }}>{formError}</p>}
        {error && <p style={{ color: 'red' }}>{error}</p>}

        <TextField
          label="Username"
          name="username"
          variant="standard"
          value={formData.username}
          size="small"
          onChange={handleChange}
        />
        <TextField
          label="Password"
          name="password"
          type="password"
          variant="standard"
          value={formData.password}
          size="small"
          onChange={handleChange}
        />
        <TextField
          label="Email"
          name="email"
          variant="standard"
          value={formData.email}
          size="small"
          onChange={handleChange}
        />
        <TextField
          label="Full Name"
          name="fullName"
          variant="standard"
          value={formData.fullName}
          size="small"
          onChange={handleChange}
        />
        <TextField
          label="Phone Number"
          name="phoneNumber"
          variant="standard"
          value={formData.phoneNumber}
          size="small"
          onChange={handleChange}
        />
        <Button type="submit" disabled={loading}>
          {loading ? 'Registering...' : 'Register'}
        </Button>
      </Card>
    </form>
  );
};

export default UserRegister;
