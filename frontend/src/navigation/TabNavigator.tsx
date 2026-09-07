import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import AgendaScreen from '../screens/agenda/AgendaScreen';
import HistoricoClienteScreen from '../screens/cliente/HistoricoClienteScreen';
import EstoqueScreen from '../screens/estoque/EstoqueScreen';
import RepassesScreen from '../screens/financeiro/RepassesScreen';
import RelatorioMensalScreen from '../screens/relatorio/RelatorioMensalScreen';
import { colors } from '../theme/colors';

const Tab = createBottomTabNavigator();

export default function TabNavigator() {
  return (
    <Tab.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: colors.surface },
        headerTintColor: colors.text,
        tabBarStyle: { backgroundColor: colors.surface, borderTopColor: colors.border },
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.textMuted,
      }}
    >
      <Tab.Screen name="Agenda" component={AgendaScreen} />
      <Tab.Screen name="Clientes" component={HistoricoClienteScreen} />
      <Tab.Screen name="Estoque" component={EstoqueScreen} />
      <Tab.Screen name="Financeiro" component={RepassesScreen} />
      <Tab.Screen name="Relatórios" component={RelatorioMensalScreen} />
    </Tab.Navigator>
  );
}
