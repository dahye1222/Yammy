import { Link } from "react-router-dom";
import "./NavigationBar.css";

const NavigationBar = () => {
  return (
    <nav className="nav-bar">
      <Link to="/">SNS</Link>
      <Link to="/useditem">중고거래</Link>
      <Link to="/prediction">승부 예측</Link>
      <Link to="/ticket">티켓 발급</Link>
    </nav>
  );
};

export default NavigationBar;
