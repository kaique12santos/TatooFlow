import React from 'react';
import { View, Text, StyleSheet, FlatList } from 'react-native';
import { Card } from '../../components/Card';
import { StatusBadge } from '../../components/StatusBadge';
import { colors } from '../../theme/colors';
import { typography } from '../../theme/typography';
import { spacing } from '../../theme/spacing';

export default function RepassesScreen() {
  const repassesMock = [
    { id: 1, tatuador: 'João Tatuador', valor: 'R$ 1.250,00', data: '05/09/2026', status: 'PAGO' },
    { id: 2, tatuador: 'Ana Artista', valor: 'R$ 980,00', data: '07/09/2026', status: 'PENDENTE' },
  ];

  return (
    <View style={styles.container}>
      <Text style={styles.header}>Repasses e Comissão</Text>
      <FlatList
        data={repassesMock}
        keyExtractor={(item) => item.id.toString()}
        renderItem={({ item }) => (
          <Card>
            <View style={styles.row}>
              <Text style={styles.tatuador}>{item.tatuador}</Text>
              <StatusBadge status={item.status} />
            </View>
            <Text style={styles.valor}>{item.valor}</Text>
            <Text style={styles.data}>Data: {item.data}</Text>
          </Card>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background, padding: spacing.md },
  header: { fontSize: typography.sizes.xl, fontWeight: typography.weights.bold, color: colors.text, marginBottom: spacing.md },
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: spacing.xs },
  tatuador: { fontSize: typography.sizes.lg, fontWeight: typography.weights.bold, color: colors.text },
  valor: { fontSize: typography.sizes.md, color: colors.secondary, fontWeight: typography.weights.bold },
  data: { fontSize: typography.sizes.xs, color: colors.textMuted, marginTop: spacing.xs },
});
