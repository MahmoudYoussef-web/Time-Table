import { describe, it, expect } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useTableFilter } from '../hooks/useTableFilter';

const data = [
  { name: 'Course A', code: 'CS101', credits: 3 },
  { name: 'Course B', code: 'CS102', credits: 4 },
  { name: 'Course C', code: 'MATH101', credits: 3 },
];

describe('useTableFilter', () => {
  it('returns all data when no filters', () => {
    const { result } = renderHook(() => useTableFilter(data, ['name', 'code']));
    expect(result.current.filtered).toHaveLength(3);
  });

  it('filters by search term', () => {
    const { result } = renderHook(() => useTableFilter(data, ['name', 'code']));
    act(() => result.current.setSearch('CS101'));
    expect(result.current.filtered).toHaveLength(1);
    expect(result.current.filtered[0].code).toBe('CS101');
  });

  it('filters by search in multiple fields', () => {
    const { result } = renderHook(() => useTableFilter(data, ['name', 'code']));
    act(() => result.current.setSearch('MATH'));
    expect(result.current.filtered).toHaveLength(1);
    expect(result.current.filtered[0].code).toBe('MATH101');
  });

  it('filters by filter key', () => {
    const { result } = renderHook(() => useTableFilter(data, ['name']));
    act(() => result.current.setFilters(f => ({ ...f, credits: '3' })));
    expect(result.current.filtered).toHaveLength(2);
  });

  it('combines search and filter', () => {
    const { result } = renderHook(() => useTableFilter(data, ['name']));
    act(() => { result.current.setSearch('Course'); });
    act(() => { result.current.setFilters(f => ({ ...f, credits: '4' })); });
    expect(result.current.filtered).toHaveLength(1);
    expect(result.current.filtered[0].code).toBe('CS102');
  });

  it('is case insensitive', () => {
    const { result } = renderHook(() => useTableFilter(data, ['name']));
    act(() => result.current.setSearch('course b'));
    expect(result.current.filtered).toHaveLength(1);
  });
});