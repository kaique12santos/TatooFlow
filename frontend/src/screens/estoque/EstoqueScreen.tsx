import React from 'react';
import { View, Text, StyleSheet, FlatList } from 'react-native';
import { Card } from '../../components/Card';
import { colors } from '../../theme/colors';
import { typography } from '../../theme/typography';
import { spacing } from '../../theme/spacing';

export default function EstoqueScreen() {
  const estoqueMock = [
    { id: 1, nome: 'Agulha 3RL Cartucho', quantidade: 45, min: 10, unidade: 'cx' },
    { id: 2, nome: 'Tinta Dynamic Black', quantidade: 3, min: 5, unidade: 'frascos' },
  ];

  return (
    <View style={styles.container}>
      <Text style={styles.header}>Controle de Estoque</Text>
      <FlatList
        data={estoqueMock}
        keyExtractor={(item) => item.id.toString()}
        renderItem={({ item }) => (
          <Card>
            <Text style={styles.nome}>{item.nome}</Text>
            <Text style={[styles.qtd, item.quantidade <= item.min ? styles.alerta : null]}>
              Qtd: {item.quantidade} {item.unidade} (Mínimo: {item.min})
            </Text>
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
  qtd: { fontSize: typography.sizes.sm, color: colors.textSecondary, marginTop: spacing.xs },
  alerta: { color: colors.danger, fontWeight: typography.weights.bold },
});
