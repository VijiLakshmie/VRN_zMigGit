import './App.css';
import Button from './Components/Button';
import Container from './Components/Container';
import { useState } from 'react';

import { MdOutlineSlowMotionVideo } from "react-icons/md";
import { FaFileUpload, FaDatabase, FaSyncAlt, FaCheckCircle } from "react-icons/fa";
import { TbArrowsTransferUp } from "react-icons/tb";
import { VscSymbolMisc } from "react-icons/vsc";
import { MdDeleteSweep } from "react-icons/md";
import { TiArrowRightThick } from "react-icons/ti";

function App() {
  const [selectedAction, setSelectedAction] = useState('');

  const [fileUploaded, setFileUploaded] = useState(false);
  const [loadedToDB, setLoadedToDB] = useState(false);
  const [transformed, setTransformed] = useState(false);

  const handleAction = (action) => {
    if (action === 'load' && !fileUploaded) {
      alert('⚠️ Please upload a file first (Endeavor Extract).');
      return;
    }

    if (action === 'transform' && !loadedToDB) {
      alert('⚠️ Please load to database first.');
      return;
    }

    if (action === 'mapping' && !loadedToDB){
      alert('⚠️ Please load to database first.');
      return;
    }

    // if (action === 'refresh' && !transformed){
    //   alert('⚠️ Please transform source to target first.');
    //   return;
    // }

    // if (action === 'validate' && !transformed){
    //   alert('⚠️ Please transform source to target first.');
    //   return;
    // }

    setSelectedAction(action);
  };

  const buttons = [
    { label: 'SCM Source', key: 'source', icon: <MdOutlineSlowMotionVideo /> },
    { label: 'Endeavor Extract', key: 'extract', icon: <FaFileUpload /> },
    { label: 'Load DB', key: 'load', icon: <FaDatabase /> },
    { label: 'Mapping', key: 'mapping', icon: <VscSymbolMisc /> },
    { label: 'Transform', key: 'transform', icon: <TbArrowsTransferUp /> },
    { label: 'Cleanup Transform', key: 'refresh', icon: <MdDeleteSweep /> },
    { label: 'Validate & Report', key: 'validate', icon: <FaCheckCircle /> },
  ];

  return (
    <>
      <header className="heading">
        <h1 className="heading_style">zMigGIT</h1>
      </header>

      <div className="btnsandcontainer">
        <div className="Btns_container">
          {buttons.map(({ label, key, icon }) => (
            <div className="btn_with_arrow" key={key}>
              <Button
                label={
                  <span className="btn_with_icon">
                    {icon} {label}
                  </span>
                }
                onClick={() => handleAction(key)}
                active={selectedAction === key}
              />
              {selectedAction === key && <span className="arrow_mark"><TiArrowRightThick /></span>}
            </div>
          ))}
        </div>

        {selectedAction ? (
          <Container
            action={selectedAction}
            fileUploaded={fileUploaded}
            setFileUploaded={setFileUploaded}
            loadedToDB={loadedToDB}
            setLoadedToDB={setLoadedToDB}
            transformed={transformed}
            setTransformed={setTransformed}
          />
        ) : (
          <div className="welcome_message">
            <h2>Welcome to zMigGIT</h2>
            <p>Please select an action to get started.</p>
          </div>
        )}
      </div>

      <footer className="footer">
        <p>© 2025 zMigGIT. All rights reserved.</p>
      </footer>
    </>
  );
}

export default App;
