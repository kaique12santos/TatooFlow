import React from 'react';
import { View, Text, StyleSheet, FlatList } from 'react-native';
import { Card } from '../../components/Card';
import { colors } from '../../theme/colors';
import { typography } from '../../theme/typography';
import { spacing } from '../../theme/spacing';

export default function HistoricoClienteScreen() {
  const clientesMock = [
    { id: 1, nome: 'Mariana Silva', telefone: '(11) 99999-8888', instagram: '@mari_silva' },
    { id: 2, nome: 'Carlos Eduardo', telefone: '(11) 97777-6666', instagram: '@carlos_tattoo' },
  ];

  return (
    <View style={styles.container}>
      <Text style={styles.header}>Histórico de Clientes</Text>
      <FlatList
        data={clientesMock}
        keyExtractor={(item) => item.id.toString()}
        renderItem={({ item }) => (
          <Card>
            <Text style={styles.nome}>{item.nome}</Text>
            <Text style={styles.info}>{item.telefone} • {item.instagram}</Text>
          </Card>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background, padding: spacing.md },
  header: { fontSize: typography.sizes.xl, fontWeight: typography.weights.bold, color: colors.text, marginBottom: spacing.md },
  nome: { fontSize: typography.sizes.lg, fontWeight: typography.weights.bold, color: colors.text },
  info: { fontSize: typography.sizes.sm, color: colors.textSecondary, marginTop: spacing.xs },
});
