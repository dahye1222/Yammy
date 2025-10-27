import { Link } from "react-router-dom";
import "./NavigationBar.css";

const NavigationBarTop = () => {
  return (
    <nav className="nav-bar-top">
      <Link to="/">Yammy</Link>
    </nav>
  );
};

export default NavigationBarTop;
