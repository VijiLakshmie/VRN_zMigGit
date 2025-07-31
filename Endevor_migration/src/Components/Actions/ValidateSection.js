import { useState } from 'react';
import './styles/validate.css';
import { TbReport } from "react-icons/tb";

function ValidateSection() {
  const [loadingSize, setLoadingSize] = useState(false);
  const [loadingCount, setLoadingCount] = useState(false);

  const generateReport = (type) => {
    if (type === "size") {
      setLoadingSize(true);
      setTimeout(() => {
        setLoadingSize(false);
        window.open("http://localhost:9090/reports/filesize", "_blank");
      }, 1500);
    } else {
      setLoadingCount(true);
      setTimeout(() => {
        setLoadingCount(false);
        window.open("http://localhost:9090/reports/filecount", "_blank");
      }, 1500);
    }
  };

  return (
    <div className="validate_container">
      <h2> Validation Reports</h2>

      <div className="report_card">
        <div className="report_info">
          <span role="img" aria-label="document">📄</span>
          <h3>File Size Report</h3>
        </div>
        {loadingSize ? (
          <div className="spinner_small" />
        ) : (
          <button onClick={() => generateReport("size")} className="report_button">
           <TbReport /> Generate Report
          </button>
        )}
      </div>

      <div className="report_card">
        <div className="report_info">
          <span role="img" aria-label="document">📄</span>
          <h3>File Count Report</h3>
        </div>
        {loadingCount ? (
          <div className="spinner_small" />
        ) : (
          <button onClick={() => generateReport("count")} className="report_button">
            <TbReport /> Generate Report
          </button>
        )}
      </div>
    </div>
  );
}

export default ValidateSection;
