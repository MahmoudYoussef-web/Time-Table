import { Pencil, Trash2, FileX } from 'lucide-react';
import { motion } from 'framer-motion';
import { Spinner } from './Spinner';

interface Column<T> {
  key: string;
  header: string;
  render?: (item: T) => React.ReactNode;
}

interface TableProps<T> {
  columns: Column<T>[];
  data: T[];
  onEdit?: (item: T) => void;
  onDelete?: (item: T) => void;
  loading?: boolean;
  emptyMessage?: string;
  keyField?: keyof T;
}

const container = { hidden: {}, show: { transition: { staggerChildren: 0.04 } } };
const rowItem = { hidden: { opacity: 0, y: 6 }, show: { opacity: 1, y: 0, transition: { duration: 0.25, ease: 'easeOut' as const } } };

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function Table<T extends { [key: string]: any }>({
  columns,
  data,
  onEdit,
  onDelete,
  loading,
  emptyMessage = 'No data found',
  keyField = 'id' as keyof T,
}: TableProps<T>) {
  if (loading) {
    return (
      <div className="flex items-center justify-center py-[76px]">
        <Spinner size="lg" />
      </div>
    );
  }

  if (!data.length) {
    return (
      <div className="flex flex-col items-center justify-center py-[76px] gap-3 text-[--muted-foreground]">
        <FileX className="w-10 h-10" />
        <p className="display-sm text-[--muted-foreground]">{emptyMessage}</p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full border-collapse">
        <thead>
          <tr className="border-b border-[--border]" style={{ borderBottomWidth: '0.5px' }}>
            {columns.map((col) => (
              <th key={col.key} className="overline text-left px-4 py-3.5 font-medium">{col.header}</th>
            ))}
            {(onEdit || onDelete) && <th className="overline text-left px-4 py-3.5 font-medium">Actions</th>}
          </tr>
        </thead>
        <motion.tbody variants={container} initial="hidden" animate="show">
          {data.map((item, idx) => (
            <motion.tr
              key={String(item[keyField] ?? idx)}
              variants={rowItem}
              className="group border-b border-[--border] hover:bg-[--muted]/40 transition-colors"
              style={{ borderBottomWidth: '0.5px' }}
            >
              {columns.map((col) => (
                <td key={col.key} className="px-4 py-3.5 body-md">
                  {col.render ? col.render(item) : String(item[col.key] ?? '')}
                </td>
              ))}
              {(onEdit || onDelete) && (
                <td className="px-4 py-3.5">
                  <div className="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    {onEdit && (
                      <button onClick={() => onEdit(item)} className="cursor-pointer hover:opacity-70">
                        <Pencil size={16} className="text-[--muted-foreground]" />
                      </button>
                    )}
                    {onDelete && (
                      <button onClick={() => onDelete(item)} className="cursor-pointer hover:opacity-70">
                        <Trash2 size={16} className="text-[--destructive]" />
                      </button>
                    )}
                  </div>
                </td>
              )}
            </motion.tr>
          ))}
        </motion.tbody>
      </table>
    </div>
  );
}
