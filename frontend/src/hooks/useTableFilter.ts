import { useState, useMemo } from 'react';

interface FilterState {
  [key: string]: string | undefined;
}

interface UseTableFilterResult<T> {
  filtered: T[];
  search: string;
  setSearch: (val: string) => void;
  filters: FilterState;
  setFilters: (val: FilterState | ((prev: FilterState) => FilterState)) => void;
  clearFilters: () => void;
}

export function useTableFilter<T>(
  data: T[] = [],
  searchFields: string[] = [],
): UseTableFilterResult<T> {
  const [search, setSearch] = useState('');
  const [filters, setFilters] = useState<FilterState>({});

  const filtered = useMemo(() => {
    return data.filter(item => {
      const record = item as Record<string, unknown>;
      const matchesSearch = !search || searchFields.some(field =>
        String(record[field] ?? '').toLowerCase().includes(search.toLowerCase()),
      );
      const matchesFilters = Object.entries(filters).every(([key, val]) =>
        !val || String(record[key] ?? '') === val,
      );
      return matchesSearch && matchesFilters;
    });
  }, [data, search, filters]);

  const clearFilters = () => setFilters({});

  return { filtered, search, setSearch, filters, setFilters, clearFilters };
}
