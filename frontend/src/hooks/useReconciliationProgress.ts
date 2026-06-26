import { useState, useEffect, useRef } from 'react';
import { ReconciliationResult } from '../services/reconciliationService';

interface ProgressData {
  percentage: number;
  message: string;
}

export function useReconciliationProgress(sessionId: string | null) {
  const [progress, setProgress] = useState<ProgressData>({ percentage: 0, message: 'Aguardando...' });
  const [result, setResult] = useState<ReconciliationResult | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isComplete, setIsComplete] = useState(false);
  const eventSourceRef = useRef<EventSource | null>(null);

  useEffect(() => {
    if (!sessionId) return;

    const eventSource = new EventSource(`/api/reconcile/progress/${sessionId}`);
    eventSourceRef.current = eventSource;

    eventSource.addEventListener('progress', (event: MessageEvent) => {
      const data = JSON.parse(event.data);
      setProgress({ percentage: data.percentage, message: data.message });
    });

    eventSource.addEventListener('complete', (event: MessageEvent) => {
      const data = JSON.parse(event.data);
      setResult(data);
      setIsComplete(true);
      eventSource.close();
    });

    eventSource.addEventListener('error', (event: MessageEvent) => {
      const data = event.data ? JSON.parse(event.data) : 'Erro desconhecido';
      setError(typeof data === 'string' ? data : data.message || 'Erro no processamento');
      eventSource.close();
    });

    return () => {
      eventSource.close();
    };
  }, [sessionId]);

  return { progress, result, error, isComplete };
}