import { Routes, Route } from "react-router-dom";
import SnsPage from "../sns/components/SNSPage";
import UsedItemPage from "../useditem/UsedItemPage";
import UsedItemDetail from "../useditem/components/UsedItemDetail"
import UsedItemEdit from "../useditem/components/UsedItemEdit"
import UsedItemCreate from "../useditem/components/UsedItemCreate"

export default function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<SnsPage />} />
      <Route path="/useditem" element={<UsedItemPage />} />
      <Route path="/useditem/:id" element={<UsedItemDetail />} />
      <Route path="/useditem/edit/:id" element={<UsedItemEdit />} />
      <Route path="/useditem/create" element={<UsedItemCreate />} />
    </Routes>
  );
}
