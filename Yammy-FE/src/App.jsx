import { Routes, Route } from 'react-router-dom';
import NavigationBarBottom from './shared/components/NavagationBarBottom'
import NavigationBarTop from './shared/components/NavigationBarTop'
<<<<<<< HEAD
import SNSPage from './sns/components/SNSPage'
=======
>>>>>>> 59bdc9e4e32c06a840c75bf2a69515a066c7525d
import UsedItem from './useditem/components/UsedItem'
import "./App.css"

function App() {
  return (
    <div className="app-container">
      <NavigationBarTop />
<<<<<<< HEAD
      <Routes>
        <Route path="/" element={<SNSPage />} />
        <Route path="/useditem" element={<UsedItem />} />
      </Routes>
=======
      <UsedItem />
>>>>>>> 59bdc9e4e32c06a840c75bf2a69515a066c7525d
      <NavigationBarBottom />
    </div>
  );
}

export default App;
