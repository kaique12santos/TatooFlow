import React, { useState } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Input } from '../../components/Input';
import { colors } from '../../theme/colors';
import { typography } from '../../theme/typography';
import { spacing } from '../../theme/spacing';

export default function BuscaClienteScreen() {
  const [busca, setBusca] = useState('');

  return (
    <View style={styles.container}>
      <Text style={styles.header}>Buscar Cliente</Text>
      <Input placeholder="Nome, telefone ou CPF..." value={busca} onChangeText={setBusca} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background, padding: spacing.md },
  header: { fontSize: typography.sizes.xl, fontWeight: typography.weights.bold, color: colors.text, marginBottom: spacing.md },
});
