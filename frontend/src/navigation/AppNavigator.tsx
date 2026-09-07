import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { useAuth } from '../hooks/useAuth';
import AuthNavigator from './AuthNavigator';
import TabNavigator from './TabNavigator';

export default function AppNavigator() {
  const { signed } = useAuth();

  return (
    <NavigationContainer>
      {signed ? <TabNavigator /> : <AuthNavigator />}
    </NavigationContainer>
  );
}
