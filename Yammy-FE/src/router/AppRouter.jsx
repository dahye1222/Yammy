import { Routes, Route } from 'react-router-dom';

import SNSPage from "../sns/components/SNSPage"
import UsedItem from "../useditem/components/UsedItem"

const AppRouter = () => {
  return (
    <Routes>
      <Route path="/" element={<SNSPage />} />
      <Route path="/useditem" element={<UsedItem />} />
    </Routes>
  );
};

export default AppRouter;