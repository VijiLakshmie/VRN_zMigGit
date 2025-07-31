import './Styles/Button.css';

function Button({ label, onClick, active }) {
  return (
    <button
      className={`btns ${active ? 'btns-active' : ''}`}
      onClick={onClick}
    >
      {label}
    </button>
  );
}

export default Button;
