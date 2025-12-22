import { useEffect, useRef, useState } from 'react';
import './SearchInput.css';

const MIN_SEARCH_CHARS = 3;
const DEBOUNCE_MS = 400;

/**
 * Unified Search Input Component
 * Provides consistent search behavior across the application:
 * - Minimum 3 characters requirement
 * - 400ms debounce
 * - Clear button
 * - Helper text
 * - Loading state
 */
export default function SearchInput({
  placeholder = 'Search...',
  onSearch,
  disabled = false,
  showResults = false,
  children,
  className = '',
  icon = '🔍',
  connectionIndicator = null,
}) {
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const debounceRef = useRef(null);
  const activeSearchIdRef = useRef(0);

  const isEligible = query.trim().length >= MIN_SEARCH_CHARS;

  useEffect(() => {
    const trimmed = query.trim();

    if (trimmed.length < MIN_SEARCH_CHARS) {
      setLoading(false);
      return;
    }

    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }

    setLoading(true);

    debounceRef.current = setTimeout(async () => {
      const searchId = ++activeSearchIdRef.current;

      try {
        await onSearch(trimmed, searchId);
      } finally {
        if (searchId === activeSearchIdRef.current) {
          setLoading(false);
        }
      }
    }, DEBOUNCE_MS);

    return () => clearTimeout(debounceRef.current);
  }, [query, onSearch]);

  const handleClear = () => {
    setQuery('');
    setLoading(false);
  };

  const charsRemaining = Math.max(0, MIN_SEARCH_CHARS - query.trim().length);

  return (
    <div className={`search-input-component ${className}`}>
      <div className="ds-input-wrapper">
        <span className="ds-input-icon ds-input-icon-left">{icon}</span>
        <input
          type="text"
          className="ds-input ds-input-with-icon-left ds-input-with-icon-right"
          placeholder={placeholder}
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          disabled={disabled}
        />
        {query && (
          <button
            type="button"
            className="ds-input-clear"
            onClick={handleClear}
            aria-label="Clear search"
          >
            ✕
          </button>
        )}
        {connectionIndicator && (
          <span className="search-connection-indicator">{connectionIndicator}</span>
        )}
      </div>

      {/* Helper text when not enough characters */}
      {query && !isEligible && charsRemaining > 0 && (
        <div className="ds-helper-text">
          Type {charsRemaining} more character{charsRemaining !== 1 ? 's' : ''} to search
        </div>
      )}

      {/* Results container */}
      {showResults && isEligible && (
        <div className="search-results-wrapper">
          {loading && (
            <div className="ds-state">
              <div className="ds-spinner" style={{ margin: '0 auto' }}></div>
            </div>
          )}
          {!loading && children}
        </div>
      )}
    </div>
  );
}

