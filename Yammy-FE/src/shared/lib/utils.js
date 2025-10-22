// Common utility functions

export const cn = (...classes) => {
  return classes.filter(Boolean).join(' ');
};

export const formatDate = (date) => {
  return new Date(date).toLocaleDateString('ko-KR');
};

export const debounce = (func, wait) => {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
};
