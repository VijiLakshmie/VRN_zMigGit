import { useState } from 'react';
import './styles/transform.css';
import { TbTransitionRightFilled } from "react-icons/tb";

function TransformSection({ setLoading, setSuccess, setTransformOutput }) {
  const [targetPlatform, setTargetPlatform] = useState('');
  const [sourcePlatform, setSourcePlatform] = useState('');
  const [progress, setProgress] = useState(0);
  const [loadingState, setLoadingState] = useState(false);
  const [successState, setSuccessState] = useState(false);
  const [backendMessage, setBackendMessage] = useState('');

  const TOTAL_TIME = 23000;  // ✅ Updated from 26000 to 23000 (23 seconds)
  const FAST_FORWARD_TIME = 100;
  const STEP_INTERVAL = 100;

  let intervalId = null;

  const handleSubmit = async () => {
    console.log("Submit clicked");
    console.log("Source:", sourcePlatform);
    console.log("Target:", targetPlatform);

    if (!sourcePlatform || !targetPlatform) {
      alert("⚠ Please select both Source and Target platforms.");
      return;
    }

    setLoading(true);
    setLoadingState(true);
    setSuccess(false);
    setSuccessState(false);
    setProgress(0);

    const startTime = Date.now();

    intervalId = setInterval(() => {
      setProgress((prev) => {
        const elapsed = Date.now() - startTime;
        const expectedProgress = (elapsed / TOTAL_TIME) * 100;
        if (prev >= 100 || expectedProgress >= 100) {
          clearInterval(intervalId);
          return prev;
        }
        return Math.min(expectedProgress, 100);
      });
    }, STEP_INTERVAL);

    const payload = {
      sourcePlatform,
      platform: targetPlatform,
      type: "target",
    };

    try {
      const response = await fetch("http://localhost:9090/transform", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      const elapsedAtResponse = Date.now() - startTime;
      const result = await response.text();

      setTransformOutput(result);
      setBackendMessage(result);

      // Accelerate to 100%
      const currentProgress = (elapsedAtResponse / TOTAL_TIME) * 100;
      const steps = 30;
      const increment = (100 - currentProgress) / steps;

      let count = 0;
      clearInterval(intervalId);

      const fastInterval = setInterval(() => {
        count++;
        setProgress((prev) => Math.min(prev + increment, 100));
        if (count >= steps) {
          clearInterval(fastInterval);
          setProgress(100);
          setLoading(false);
          setLoadingState(false);
        }
      }, FAST_FORWARD_TIME / steps);

      if (response.ok && result.toLowerCase().includes("completed" || "invalid")) {
        setSuccess(true);
        setSuccessState(true);
      } else {
        alert("⚠ Transform ran but may not be successful.");
      }
    } catch (error) {
      console.error("Transform error:", error);
      alert("🚫 Error during transformation");
      clearInterval(intervalId);
      setLoading(false);
      setLoadingState(false);
    }
  };

  return (
    <div className="load_section">
      {loadingState ? (
        <>
          <div className="transform_progress_container">
            <div
              className="transform_progress_bar"
              style={{ width: `${progress}%` }}
            ></div>
          </div>
          <p className="loading_text">
            Transforming from {sourcePlatform} to {targetPlatform}... ({Math.floor(progress)}%)
          </p>
        </>
      ) : successState ? (
        <p className="success_text">{backendMessage}</p>
      ) : (
        <>
          <h2>Select Source and Target Platform for Transformation</h2><br></br><br></br>
          <div className="select_wrapper">
            <div>
              <select
                className="select_bar"
                value={sourcePlatform}
                onChange={(e) => setSourcePlatform(e.target.value)}
              >
                <option value="">-- SOURCE --</option>
                <option value="ENDEVOR">ENDEVOR</option>
                <option value="CHANGEMAN">CHANGEMAN</option>
              </select>
            </div>
            <div>
              <select
                className="select_bar"
                value={targetPlatform}
                onChange={(e) => setTargetPlatform(e.target.value)}
              >
                <option value="">-- TARGET --</option>
                <option value="GITHUB">GITHUB</option>
                <option value="GITLAB">GITLAB</option>
                <option value="Bitbucket">BitBucket</option>
                <option value="Azure_DevOps">Azure DevOps</option>
              </select>
            </div>
          </div>
          <button
            className="transformbtns"
            onClick={handleSubmit}
            disabled={loadingState}
          >
          <TbTransitionRightFilled /> Transform from {sourcePlatform || '...'} to {targetPlatform || '...'}
          </button>
        </>
      )}
    </div>
  );
}

export default TransformSection;
