import './Styles/Container.css';
import { useState, useEffect, useCallback } from "react";
import ExtractSection from "./Actions/ExtractSection";
import LoadSection from "./Actions/LoadSection";
import TransformSection from "./Actions/TransformSection.js";
import ValidateSection from "./Actions/ValidateSection";
import VideoSection from "./Actions/VideoSection";
import Mapping from "./Actions/Mapping";
import RefreshSection from './Actions/RefreshSection';

function Container({
  action,
  fileUploaded,
  setFileUploaded,
  loadedToDB,
  setLoadedToDB,
  transformed,
  setTransformed
}) {
  const [selectedFile, setSelectedFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [platform, setPlatform] = useState('');
  const [transformOutput, setTransformOutput] = useState('');

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file && file.type === "application/json") {
      setSelectedFile(file);
      setFileUploaded(true);
      setSuccess(false);
    } else {
      alert("Please upload a valid JSON file.");
    }
  };

  const handleLoadToMongo = useCallback(async () => {
    const formData = new FormData();
    formData.append("file", selectedFile);

    try {
      return await fetch("http://localhost:9090/extract", {
        method: "POST",
        body: formData,
      });
    } catch (error) {
      console.error("Mongo Upload Error:", error);
      throw error;
    }
  }, [selectedFile]);

  const handleLoadToDB2 = useCallback(async () => {
    const formData = new FormData();
    formData.append("file", selectedFile);

    try {
      return await fetch("http://localhost:9090/extractdb2", {
        method: "POST",
        body: formData,
      });
    } catch (error) {
      console.error("DB2 Upload Error:", error);
      throw error;
    }
  }, [selectedFile]);

  const handleValidation = useCallback(async () => {
    setLoading(true);
    setSuccess(false);

    try {
      const response = await fetch('http://localhost:9090/validate', {
        method: 'GET',
      });

      if (response.ok) {
        const result = await response.text();
        console.log("Validation Result:", result);
        setSuccess(true);
      } else {
        alert("❌ Validation failed.");
      }
    } catch (err) {
      console.error("Validation error:", err);
      alert("🚫 Backend error during validation.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (action === 'validate') {
      handleValidation();
    }
  }, [action, handleValidation]);

  return (
    <div className="content_display">
      {action === 'source' && <VideoSection />}

      {action === 'extract' && (
        <ExtractSection selectedFile={selectedFile} handleFileChange={handleFileChange} />
      )}

      {action === 'load' && (
        <LoadSection
          selectedFile={selectedFile}
          onLoadMongo={handleLoadToMongo}
          onLoadDB2={handleLoadToDB2}
          setLoadedToDB={setLoadedToDB} 
        />
      )}
      {action === 'transform' && (
        <TransformSection
          setLoading={setLoading}
          setSuccess={setSuccess}
          setTransformOutput={setTransformOutput}
          setTransformed={setTransformed}  // ✅ Add this line
        />
      )}

      {action === 'mapping' && <Mapping />}

      {action === 'refresh' && (
        <RefreshSection
          setFileUploaded={setFileUploaded}
          setLoadedToDB={setLoadedToDB}
          setTransformed={setTransformed}
          setSelectedFile={setSelectedFile}
          setPlatform={setPlatform}
        />
      )}

      {action === 'validate' && (
        <ValidateSection loading={loading} success={success} />
      )}
    </div>
  );
}

export default Container;
