import React from 'react';
import { View, Text, StyleSheet, FlatList } from 'react-native';
import { Card } from '../../components/Card';
import { StatusBadge } from '../../components/StatusBadge';
import { EmptyState } from '../../components/EmptyState';
import { colors } from '../../theme/colors';
import { typography } from '../../theme/typography';
import { spacing } from '../../theme/spacing';

export default function AgendaScreen() {
  const agendamentosMock = [
    { id: 1, cliente: 'Mariana Silva', horario: '14:00 - 17:00', estilo: 'Blackwork Realista', status: 'AGENDADO' },
    { id: 2, cliente: 'Carlos Eduardo', horario: '17:30 - 19:00', estilo: 'Old School', status: 'EM_ANDAMENTO' },
  ];

  return (
    <View style={styles.container}>
      <Text style={styles.header}>Agenda do Dia</Text>
      <FlatList
        data={agendamentosMock}
        keyExtractor={(item) => item.id.toString()}
        ListEmptyComponent={<EmptyState message="Nenhum agendamento para hoje." />}
        renderItem={({ item }) => (
          <Card>
            <View style={styles.row}>
              <Text style={styles.cliente}>{item.cliente}</Text>
              <StatusBadge status={item.status} />
            </View>
            <Text style={styles.detalhes}>{item.horario} • {item.estilo}</Text>
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
  cliente: { fontSize: typography.sizes.lg, fontWeight: typography.weights.bold, color: colors.text },
  detalhes: { fontSize: typography.sizes.sm, color: colors.textSecondary },
});
